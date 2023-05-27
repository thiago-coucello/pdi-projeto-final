clc;
close all;
clear all;
pkg load image;

function ring_se = generate_ring_se(inner_ring_se, inner_ring_radius, outer_ring_se, outer_ring_radius)
  ring_se_matrix = getnhood(outer_ring_se) - padarray(getnhood(inner_ring_se), repmat(outer_ring_radius - inner_ring_radius, 1, 2));
  ring_se = strel("arbitrary", ring_se_matrix);
endfunction

% Carregando imagem de sangue original
original_image = imread("Dataset/0a7bfa8a-ee52-4f7a-b9c5-2919ecfa93ef.png");
%original_image = imread("Dataset/0a747cb3-c720-4572-a661-ab5670a5c42e.png");
[lin, col, ~] = size(original_image);
resizing_ratio = 4;
original_image = imresize(original_image, [lin/resizing_ratio, col/resizing_ratio]);

hsv_image = rgb2hsv(original_image);
H = hsv_image(:, :, 1);
S = hsv_image(:, :, 2);

adjusted_s = imadjust(S, [0.23, 1], [0, 1]);

figure,
subplot(1, 2, 1); imshow(S);
subplot(1, 2, 2); imshow(adjusted_s);

S = adjusted_s;

hue_threshold = graythresh(H, "otsu");
saturation_threshold = graythresh(S, "otsu");

binary_h = im2bw(H, hue_threshold);
binary_s = im2bw(S, saturation_threshold);

marker_image = binary_h .* binary_s;

figure,
subplot(2, 2, 1); imshow(hsv_image); title("Imagem HSV");
subplot(2, 2, 2); imshow(binary_h); title("Matiz Binarizada");
subplot(2, 2, 3); imshow(binary_s); title("Saturação Binarizada");
subplot(2, 2, 4); imshow(marker_image); title("Imagem de Marcadores");

target_objects = bwboundaries(marker_image);

figure,
imshow(original_image);
hold on;

for k = 1 : length(target_objects)
  target = target_objects{k};
  target_x = target(:, 2);
  target_y = target(:, 1);
  plot(target_x, target_y, "r");
endfor

hold off;
