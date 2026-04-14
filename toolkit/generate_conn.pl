#!/usr/bin/perl
use strict;
use warnings;
use Getopt::Long;

# 1. Nilai Bawaan (Default) jika parameter tidak diisi di terminal
my $num_nodes = 100;
my $sim_time = 2592000;
my $total_encounters = 10000;
my $file_name = "data/events/koneksi_custom.txt"; 

# 2. Mengambil argumen dari Command Line
GetOptions(
    "nrof=i"   => \$num_nodes,
    "time=i"   => \$sim_time,
    "events=i" => \$total_encounters,
    "out=s"    => \$file_name
) or die "Gagal membaca argumen.\nCara Penggunaan: perl generate_conn.pl -nrof <jumlah_node> -time <waktu_detik> -events <jumlah_pertemuan> -out <nama_file>\n";

my @events;

print "Mulai menghasilkan data kejadian koneksi...\n";
print "-> Jumlah Node: $num_nodes\n";
print "-> Waktu Simulasi: $sim_time detik\n";
print "-> Total Pertemuan: $total_encounters\n";
print "-> File Output: $file_name\n\n";

for (my $i = 0; $i < $total_encounters; $i++) {
    # Mengundi dua node yang saling bertemu sesuai jumlah node (-nrof)
    my $node1 = int(rand($num_nodes));
    my $node2 = int(rand($num_nodes));
    
    # Pastikan node tidak terhubung dengan dirinya sendiri
    while ($node1 == $node2) {
        $node2 = int(rand($num_nodes));
    }
    
    # Menentukan waktu awal koneksi (mencegah minus jika waktu simulasi terlalu singkat)
    my $max_start_time = $sim_time - 3600;
    $max_start_time = $sim_time if ($max_start_time < 0); 
    my $time_up = rand($max_start_time);
    
    # Menentukan durasi pertemuan (diundi antara 10 detik hingga 1 jam)
    my $duration = 10 + rand(3600 - 10);
    my $time_down = $time_up + $duration;
    
    # Membulatkan waktu menjadi 1 angka di belakang koma
    $time_up = sprintf("%.1f", $time_up);
    $time_down = sprintf("%.1f", $time_down);
    
    # Memasukkan kejadian UP dan DOWN ke daftar array
    push @events, { time => $time_up, str => "$time_up CONN $node1 $node2 up" };
    push @events, { time => $time_down, str => "$time_down CONN $node1 $node2 down" };
}

# 3. Mengurutkan Kejadian (ONE Simulator mewajibkan urutan waktu dari terkecil ke terbesar)
@events = sort { $a->{time} <=> $b->{time} } @events;

# 4. Menyimpan hasil ke dalam file teks (-out)
open(my $fh, '>', $file_name) or die "Tidak dapat membuat file '$file_name' $!";
foreach my $ev (@events) {
    print $fh $ev->{str} . "\n";
}
close $fh;

my $total_lines = scalar @events;
print "Selesai! File '$file_name' berhasil dibuat dengan $total_lines baris perintah.\n";