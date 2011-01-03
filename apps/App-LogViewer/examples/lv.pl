#!/usr/bin/env perl

use 5.010;
use utf8;
use strict;
use warnings;
use autodie;
use DateTime;
use App::LogViewer;
use Glib qw(TRUE FALSE);
use Gtk2 '-init';
use Acme::Gtk2::Ex::Builder;

my $lv = App::LogViewer->new(
    channel => 'perl-kr',
    date    => DateTime->now(time_zone => 'Asia/Seoul'),
);
$lv->channel('perl-kr');
$lv->date(DateTime->now(time_zone => 'Asia/Seoul'));

my $app = build {
    widget Window => contain {
        info id             => 'window';
        set  title          => 'Seoul.pm irc log viewer';
        set  default_size   => 640, 480;
        set  position       => 'center';
        on   delete_event   => sub { Gtk2->main_quit };

        widget VBox => contain {
            info id => 'vbox';
            widget Label => contain {
                info id        => 'main-label';
                set  markup    =>
                      '<span size="large">'
                    . 'IRC log'
                    . '</span>'
                    ;
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
            };
        };
    };
};

$app->find('vbox')->pack_start($lv->get_talk_vbox, TRUE, TRUE, 1);
$app->find('window')->show_all;

Gtk2->main;

sub change_label {
    my $lv = shift;

    $app->find('main-label')->set_label($lv->date);
}

sub prev_log {
    my $self = shift;
    my $lv   = shift;

    $lv->date( $lv->date->subtract(days => 1) );
    change_label($lv);
}

sub next_log {
    my $self = shift;
    my $lv   = shift;

    $lv->date( $lv->date->add(days => 1) );
    change_label($lv);
}
