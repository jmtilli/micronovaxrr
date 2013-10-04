How to use ascii exports in various programs



Gnumeric:

gnumeric won't load ascii exports with file -> open if the decimal separator
of locale isn't ".".

The following works for gnumeric 1.6.1:

Data -> Get External Data -> Import Text File
Original data type: Separated
Forward
Separators: Space
Forward
Source Locale: North America -> United States/English (C)
Finish




Excel 2002:

File -> Open, Files of type: "Text files (*.prn; *.txt; *.csv)"
Data type: delimited
Delimiters: space
Advanced -> Decimal separator: "."





OpenOffice 2.0.0:

File -> Open
File type: Text CSV (*.csv;*.txt)
Separated by: Space
Change the column type of every column to "US English"




gnuplot 4.0:

##plot to postscript file
#set terminal postscript eps enhanced color "Helvetica" 14
#set output 'plot.eps'

##plot to pdf file
#set terminal pdf enhanced fname "Helvetica" fsize 14
#set output 'plot.pdf'

##plot to png file
#set terminal png size 800,600 transparent enhanced
#set output 'plot.png'

set xlabel 'alpha (°)'
set ylabel 'I/I0 (dB)'
plot 'asciiexport.txt' using 1:(10*log($2)/log(10)) \
     title 'simulated' with linespoints,            \
     'asciiexport.txt' using 1:(10*log($3)/log(10)) \
     title 'measured' with linespoints
