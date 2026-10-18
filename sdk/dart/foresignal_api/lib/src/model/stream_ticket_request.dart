//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stream_ticket_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StreamTicketRequest {
  /// Returns a new [StreamTicketRequest] instance.
  StreamTicketRequest({

    required  this.purpose,

     this.paths,

     this.ttl,
  });

  @JsonKey(
    
    name: r'purpose',
    required: true,
    includeIfNull: false,
  )


  final StreamTicketRequestPurposeEnum purpose;



      /// Optional narrower set of paths (within the purpose's paths) the ticket may be used on
  @JsonKey(
    
    name: r'paths',
    required: false,
    includeIfNull: false,
  )


  final List<String>? paths;



      /// Tile tickets only: lifetime, default 10m, max 1h (ISO-8601 or shorthand)
  @JsonKey(
    
    name: r'ttl',
    required: false,
    includeIfNull: false,
  )


  final String? ttl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StreamTicketRequest &&
      other.purpose == purpose &&
      other.paths == paths &&
      other.ttl == ttl;

    @override
    int get hashCode =>
        purpose.hashCode +
        paths.hashCode +
        ttl.hashCode;

  factory StreamTicketRequest.fromJson(Map<String, dynamic> json) => _$StreamTicketRequestFromJson(json);

  Map<String, dynamic> toJson() => _$StreamTicketRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StreamTicketRequestPurposeEnum {
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'tiles')
tiles(r'tiles');

const StreamTicketRequestPurposeEnum(this.value);

final String value;

@override
String toString() => value;
}


