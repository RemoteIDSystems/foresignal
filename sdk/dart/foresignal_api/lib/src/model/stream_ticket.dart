//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stream_ticket.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StreamTicket {
  /// Returns a new [StreamTicket] instance.
  StreamTicket({

    required  this.ticket,

    required  this.purpose,

    required  this.expiresAt,

     this.paths,
  });

      /// `fst_` + 24 random bytes (base64url)
  @JsonKey(
    
    name: r'ticket',
    required: true,
    includeIfNull: false,
  )


  final String ticket;



  @JsonKey(
    
    name: r'purpose',
    required: true,
    includeIfNull: false,
  )


  final StreamTicketPurposeEnum purpose;



      /// Stream: deadline for the first connection (60 s after minting; afterwards reconnects work up to 5 min after the last disconnect and 24 h in total). Tiles: end of the ticket's lifetime.
  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'paths',
    required: false,
    includeIfNull: false,
  )


  final List<String>? paths;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StreamTicket &&
      other.ticket == ticket &&
      other.purpose == purpose &&
      other.expiresAt == expiresAt &&
      other.paths == paths;

    @override
    int get hashCode =>
        ticket.hashCode +
        purpose.hashCode +
        expiresAt.hashCode +
        paths.hashCode;

  factory StreamTicket.fromJson(Map<String, dynamic> json) => _$StreamTicketFromJson(json);

  Map<String, dynamic> toJson() => _$StreamTicketToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StreamTicketPurposeEnum {
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'tiles')
tiles(r'tiles');

const StreamTicketPurposeEnum(this.value);

final String value;

@override
String toString() => value;
}


