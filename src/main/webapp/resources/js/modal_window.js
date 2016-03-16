/**
 * Created by volkswagen1 on 02.03.2016.
 */

    $(document).ready(function() { // ��� ����� ����� �������� ��������
        $('a#go').click( function(event){ // ����� ���� �� ������ � id="go"

            event.preventDefault(); // ��������� ����������� ���� ��������
            $('#overlay').fadeIn(400, // ������� ������ ���������� ������ ��������
                function(){ // ����� ���������� ����������� ��������
                    $('#modal_form')
                        .css('display', 'block') // ������� � ���������� ���� display: none;
                        .animate({opacity: 1, top: '50%'}, 200); // ������ ���������� ������������ ������������ �� ���������� ����
                });
        });
        /* �������� ���������� ����, ��� ������ �� �� ����� �� � �������� ������� */
        $('#modal_close, #overlay').click( function(){ // ����� ���� �� �������� ��� ��������
            $('#modal_form')
                .animate({opacity: 0, top: '45%'}, 200,  // ������ ������ ������������ �� 0 � ������������ ������� ���� �����
                function(){ // ����� ��������
                    $(this).css('display', 'none'); // ������ ��� display: none;
                    $('#overlay').fadeOut(400); // �������� ��������
                }
            );
        });
    });
