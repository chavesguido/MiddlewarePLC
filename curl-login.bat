mkdir login
curl %1 %2 %3 %4 %5 -H "Connection: keep-alive" %6 %7 -X POST --compressed --insecure -v -o tempBad.txt 2> tempLogin.txt