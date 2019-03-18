#! /bin/sh
set -e
notBefore=`openssl x509 -in $1 -dates -noout | grep notBefore`
notBefore=${notBefore#*=}

notAfter=`openssl x509 -in $1 -dates -noout | grep notAfter`
notAfter=${notAfter#*=}

notBefore=`date -d "$notBefore" +%s`
notAfter=`date -d "$notAfter" +%s`

echo "{ \"notBefore\" : $notBefore, \"notAfter\" : $notAfter }"
