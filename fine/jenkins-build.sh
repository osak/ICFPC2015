cd $WORKSPACE
ln -sf /var/lib/ICFPC2015/src .
ln -sf /var/lib/ICFPC2015/problems .
ln -sf /var/lib/ICFPC2015/sim .
ln -sf /var/lib/ICFPC2015/Makefile .
ln -sf /var/lib/ICFPC2015/play_icfp2015 .

rm -rf visdump output

BEGIN_DATE=`date +'%s'`
make visdump/problem_0.json

REVISION=`git log --format='%H' | head -n1`
COMMENT=`git log --oneline | head -n1`
ruby2.0 /var/lib/ICFPC2015/fine/main.rb "$WORKSPACE" "$REVISION" "$COMMENT" "$BEGIN_DATE"

# Backup process
tar cvjf /var/opt/jenkins-backup/$BUILD_TAG.tar.bz2 --exclude=.git .
