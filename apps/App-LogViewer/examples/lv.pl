#!/usr/bin/env perl

use 5.010;
use utf8;
use strict;
use warnings;
use autodie;
use FindBin qw($Bin);
use File::Spec::Functions;
use Readonly;
use DateTime;
use App::LogViewer;
use Glib qw(TRUE FALSE);
use Gtk2 '-init';
use Acme::Gtk2::Ex::Builder;

Readonly::Scalar my $ICON_PATH    => catfile($Bin, 'icon.png');
Readonly::Scalar my $LABEL_PREFIX => q{<span size="large">IRC log</span>};
Readonly::Scalar my $LABEL_FORMAT => sprintf(
    q{%s<span size="large">: <b><i>%%s</i></b></span>},
    $LABEL_PREFIX
);

my $lv = App::LogViewer->new(
    channel => 'perl-kr',
    date    => DateTime->now(time_zone => 'Asia/Seoul'),
);

my $app = build {
    widget Window => contain {
        info id             => 'window';
        set  title          => 'Seoul.pm irc log viewer';
        set  default_size   => 640, 480;
        set  position       => 'center';
        set  icon_from_file => $ICON_PATH;
        on   delete_event   => sub { Gtk2->main_quit };

        widget VBox => contain {
            info id => 'vbox';
            widget Label => contain {
                info id        => 'main-label';
                set  markup    => $LABEL_PREFIX;
                set  alignment => 0, 0.5;
                set  padding   => 10, 10;
            };
            widget HBox => contain {
                widget Button => contain {
                    set label   => 'prev';
                    on  clicked => \&prev_log, $lv;
                };
                widget Button => contain {
                    set label   => 'next';
                    on  clicked => \&next_log, $lv;
                };
                widget Button => contain {
                    set label   => 'today';
                    on  clicked => \&today_log, $lv;
                };
                widget Button => contain {
                    set label   => 'force refresh';
                    on  clicked => \&force_refresh_log, $lv;
                };
            };
            widget $lv->get_talk_vbox => contain {
                info packing => TRUE, TRUE, 1, 'start';
            };
        };
    };
};

update_label($lv);

$app->find('window')->show_all;

Gtk2->main;

sub update_label {
    my $lv = shift;

    $app->find('main-label')->set_label( sprintf($LABEL_FORMAT, $lv->date->ymd) );
}

sub prev_log {
    my $self = shift;
    my $lv   = shift;

    $lv->date( $lv->date->subtract(days => 1) );
    update_label($lv);
}

sub next_log {
    my $self = shift;
    my $lv   = shift;

    $lv->date( $lv->date->add(days => 1) );
    update_label($lv);
}

sub today_log {
    my $self = shift;
    my $lv   = shift;

    $lv->date(DateTime->now(time_zone => 'Asia/Seoul'));
    update_label($lv);
}

sub force_refresh_log {
    my $self = shift;
    my $lv   = shift;

    $lv->force_reload(1);
    $lv->force_reload(0);
}
