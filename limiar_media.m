function f = limiar_media(g, m, n, b)
  pkg load image;
  [lin col] = size(g);
  x = floor(m/2); 
  g = padarray(g, [x x], 'replicate');
  f = zeros(size(g));
  #g = im2double(g);  
  for i=1:lin
    for j=1:col
      masc = g(i:(m+i)-1, j:(n+j)-1); # Janela deslizante
      media = mean([min(masc(:)) max(masc(:))]); #Média 
      if (g(i,j) > b*media )
        f(i,j) = 1;
      else
        f(i,j) = 0;
      endif      
    endfor
  endfor
  # remover padding
  [linX colX] = size(f);
  f = f(x+1:linX-x,x+1:colX-x);
  #f = uint8(f);  
endfunction
