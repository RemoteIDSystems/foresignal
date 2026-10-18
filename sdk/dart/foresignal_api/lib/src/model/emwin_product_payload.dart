//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'emwin_product_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EmwinProductPayload {
  /// Returns a new [EmwinProductPayload] instance.
  EmwinProductPayload({

    required  this.heading,

    required  this.awipsId,

     this.office,

    required  this.issuedAt,

    required  this.text,
  });

      /// WMO heading TTAAii CCCC DDHHMM
  @JsonKey(
    
    name: r'heading',
    required: true,
    includeIfNull: false,
  )


  final String heading;



  @JsonKey(
    
    name: r'awipsId',
    required: true,
    includeIfNull: false,
  )


  final String awipsId;



  @JsonKey(
    
    name: r'office',
    required: false,
    includeIfNull: false,
  )


  final String? office;



  @JsonKey(
    
    name: r'issuedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime issuedAt;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EmwinProductPayload &&
      other.heading == heading &&
      other.awipsId == awipsId &&
      other.office == office &&
      other.issuedAt == issuedAt &&
      other.text == text;

    @override
    int get hashCode =>
        heading.hashCode +
        awipsId.hashCode +
        office.hashCode +
        issuedAt.hashCode +
        text.hashCode;

  factory EmwinProductPayload.fromJson(Map<String, dynamic> json) => _$EmwinProductPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$EmwinProductPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

