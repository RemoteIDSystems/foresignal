//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate_licences_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRateLicencesInner {
  /// Returns a new [BaseRateLicencesInner] instance.
  BaseRateLicencesInner({

    required  this.source_,

    required  this.licence,

     this.egress,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'egress',
    required: false,
    includeIfNull: false,
  )


  final BaseRateLicencesInnerEgressEnum? egress;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRateLicencesInner &&
      other.source_ == source_ &&
      other.licence == licence &&
      other.egress == egress;

    @override
    int get hashCode =>
        source_.hashCode +
        licence.hashCode +
        egress.hashCode;

  factory BaseRateLicencesInner.fromJson(Map<String, dynamic> json) => _$BaseRateLicencesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateLicencesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BaseRateLicencesInnerEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const BaseRateLicencesInnerEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


