clc;
close all;
clear all;
pkg load image;

function ring_se = generate_ring_se(inner_ring_se, inner_ring_radius, outer_ring_se, outer_ring_radius)
  ring_se_matrix = getnhood(outer_ring_se) - padarray(getnhood(inner_ring_se), repmat(outer_ring_radius - inner_ring_radius, 1, 2));
  ring_se = strel("arbitrary", ring_se_matrix);
endfunction

function arr_image = arr_filter(img, arr_ring, arr_disk, outer_ring_radius, inner_ring_radius)
  img = im2double(img); % convertendo imagem para tipo double
  arr_disk_nhood = getnhood(arr_disk);  % extraindo vizinhança do elemento estruturante de disco
  arr_ring_nhood = getnhood(arr_ring);  % extraindo vizinhança do elemento estruturante de anel

  % Fazendo o padding de zeros do disco para ficar do mesmo tamanho do anel
  padded_disk = padarray(arr_disk_nhood, repmat(outer_ring_radius - inner_ring_radius, 1, 2));

  % Criando filtro de média no formato do anel
  ring_mask = arr_ring_nhood ./ sum(arr_ring_nhood(:));

  % Criando filtro de média no formato do disco
  disk_mask = padded_disk ./ sum(padded_disk(:));

  % Filtrando imagem com os dois filtros
  img_ring_filtered = imfilter(img, ring_mask, "conv");
  img_disk_filtered = imfilter(img, disk_mask, "conv");

  % Realizando cálculo do ARR
  arr_image = img_ring_filtered ./ img_disk_filtered - 1;

  % Excluindo regiões cuja intensidade seja menor que 0.05
  arr_image(arr_image < 0.05) = 0;
endfunction

arguments = argv()

file_name = arguments{1}
file_extension = strsplit(file_name, ".");
file_extension = file_extension{end}
task = arguments{2}

% Lendo imagem original
original_image = imread(file_name);

% Redimensionando para 1/4 do tamanho original para maior rapidez do código
[lin, col, ~] = size(original_image);
resizing_ratio = 4;
original_image = imresize(original_image, [lin/resizing_ratio, col/resizing_ratio]);

% Imagem convertida para nível de cinza para realização de filtragem morfológica
gray_image = rgb2gray(original_image);
binary_image = im2bw(gray_image);

if isequaln(task, "all") || isequaln(task, "blood")
  %%%%%% REALIZANDO FILTRAGEM DAS CÉLULAS VERMELHAS DO SANGUE

  % Realizando filtragem morfológica

  %% Esses raios não foram especificados corretamente no artigo
  RBC_radius = str2num(arguments{3});  % Raio das células vermelhas do sangue (Red Blood Cells - RBCs)
  WBC_radius = str2num(arguments{4});  % Raio das células brancas do sangue (White Blood Cells - WBCs)

  outer_ring_ratio = 0.70;  % Razão do anel externo do elemento estruturante
  inner_ring_ratio = 0.35;  % Razão do anel interno do elemento estruturante
  outer_ring_radius = floor(outer_ring_ratio * RBC_radius); % Anel externo é 70% do raio das RBCs
  inner_ring_radius = floor(inner_ring_ratio * RBC_radius); % Anel interno é 35% do raio das RBCs
  % Obs: arredondando pra baixo porque o raio deve ser inteiro

  outer_ring_se = strel("disk", outer_ring_radius, 0);  % Gera disco que será a parte externa do anel
  inner_ring_se = strel("disk", inner_ring_radius, 0);  % Gera disco que será a parte interna do anel

  % Função que constrói o anel com base nos dois discos
  ring_se = generate_ring_se(inner_ring_se, inner_ring_radius, outer_ring_se, outer_ring_radius);

  % Gera o disco da erosão que tem o mesmo raio do anel externo
  disk_se = strel("disk", outer_ring_radius, 0);

  dilated_image = imdilate(gray_image, ring_se); % Dilata a imagem com o anel
  eroded_image = imerode(dilated_image, disk_se); % Erode a imagem com o disco

  % Realizando transformação ARR

  % Montando anel da filtragem arr
  arr_outer_ring_radius = floor(RBC_radius * 1.25);
  arr_inner_ring_radius = floor(RBC_radius * 0.90);

  arr_outer_ring = strel("disk", arr_outer_ring_radius, 0);
  arr_inner_ring = strel("disk", arr_inner_ring_radius, 0);
  arr_ring = generate_ring_se(arr_inner_ring, arr_inner_ring_radius, arr_outer_ring, arr_outer_ring_radius);
  arr_disk = strel("disk", arr_inner_ring_radius, 0);

  % Realizando filtragem ARR
  arr_image = arr_filter(gray_image, arr_ring, arr_disk, arr_outer_ring_radius, arr_inner_ring_radius);

  % Extraindo cada artefato que seriam as células vermelhas
  blood_cells = bwboundaries(arr_image);

endif

if isequaln(task, "all") || isequaln(task, "dna")
  %%%%%%%% REALIZANDO EXTRAÇÃO DOS CORPOS DE DNA

  % Imagem convertida para HSV (Hue, Saturation e Value)
  hsv_image = rgb2hsv(original_image);

  % Extraindo matiz e saturação da imagem HSV
  hue_component = hsv_image(:, :, 1);
  saturation_component = hsv_image(:, :, 2);

  % Aumentando o contraste da saturação
  saturation_component = imadjust(saturation_component, [0.23, 1], [0, 1]);

  % Aplicando um filtro de média para suavizar ruídos
  median_filter = fspecial("average", 3);
  hue_component = imfilter(hue_component, median_filter, "conv");
  saturation_component = imfilter(saturation_component, median_filter, "conv");

  % Binarizando matiz e saturação utilizando o método de otsu
  binary_hue = im2bw(hue_component, graythresh(hue_component));
  binary_saturation = im2bw(saturation_component, graythresh(saturation_component));

  % Gerando imagem de marcadores a partir da matiz e saturação
  marker_image = binary_hue .* binary_saturation;

  % Extraindo corpos de DNA (corpos brilhantes na imagem de marcadores)
  dna_bodies = bwboundaries(marker_image);

endif

figure,

image_title = "Contorno dos corpos de DNA e células vermelhas"

if isequaln(task, "blood")
  image_title = "Contorno das células vermelhas"
elseif isequaln(task, "dna")
  image_title = "Contorno dos corpos de DNA"
endif

imshow(original_image); title(image_title);

if isequaln(task, "all") || isequaln(task, "blood")
  hold on;
  for k = 1 : length(blood_cells)
    blood_cell = blood_cells{k};
    blood_cell_x = blood_cell(:, 2);
    blood_cell_y = blood_cell(:, 1);

    plot(blood_cell_x, blood_cell_y, "-.r", "LineWidth", 1);
  endfor
  hold off;
endif

if isequaln(task, "all") || isequaln(task, "dna")
  hold on;
  for k = 1 : length(dna_bodies)
    dna_body = dna_bodies{k};
    dna_body_x = dna_body(:, 2);
    dna_body_y = dna_body(:, 1);

    plot(dna_body_x, dna_body_y, "-g", "LineWidth", 1);
  endfor
  hold off;
endif

save_file = strcat("./backend/images/processedImage.", "png");
save_mode = "-dpng"

print(save_file, save_mode);
