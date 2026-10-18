//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'emwin_warning_payload_duplicate_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EmwinWarningPayloadDuplicateOf {
  /// Returns a new [EmwinWarningPayloadDuplicateOf] instance.
  EmwinWarningPayloadDuplicateOf({

    required  this.source_,

    required  this.externalId,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final EmwinWarningPayloadDuplicateOfSource_Enum source_;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EmwinWarningPayloadDuplicateOf &&
      other.source_ == source_ &&
      other.externalId == externalId;

    @override
    int get hashCode =>
        source_.hashCode +
        externalId.hashCode;

  factory EmwinWarningPayloadDuplicateOf.fromJson(Map<String, dynamic> json) => _$EmwinWarningPayloadDuplicateOfFromJson(json);

  Map<String, dynamic> toJson() => _$EmwinWarningPayloadDuplicateOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EmwinWarningPayloadDuplicateOfSource_Enum {
@JsonValue(r'nws-alerts')
nwsAlerts(r'nws-alerts');

const EmwinWarningPayloadDuplicateOfSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


