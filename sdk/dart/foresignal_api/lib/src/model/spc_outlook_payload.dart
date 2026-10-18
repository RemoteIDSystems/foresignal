//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'spc_outlook_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SpcOutlookPayload {
  /// Returns a new [SpcOutlookPayload] instance.
  SpcOutlookPayload({

    required  this.day,

    required  this.type,

    required  this.label,

     this.label2,

    required  this.dn,

     this.issue,

     this.valid,

     this.expire,

    required  this.title,
  });

          // minimum: 1
          // maximum: 3
  @JsonKey(
    
    name: r'day',
    required: true,
    includeIfNull: false,
  )


  final int day;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final SpcOutlookPayloadTypeEnum type;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'label2',
    required: false,
    includeIfNull: false,
  )


  final String? label2;



      /// Numeric rank for rules such as dn >= 5
  @JsonKey(
    
    name: r'dn',
    required: true,
    includeIfNull: false,
  )


  final int dn;



  @JsonKey(
    
    name: r'issue',
    required: false,
    includeIfNull: false,
  )


  final DateTime? issue;



  @JsonKey(
    
    name: r'valid',
    required: false,
    includeIfNull: false,
  )


  final DateTime? valid;



  @JsonKey(
    
    name: r'expire',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expire;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SpcOutlookPayload &&
      other.day == day &&
      other.type == type &&
      other.label == label &&
      other.label2 == label2 &&
      other.dn == dn &&
      other.issue == issue &&
      other.valid == valid &&
      other.expire == expire &&
      other.title == title;

    @override
    int get hashCode =>
        day.hashCode +
        type.hashCode +
        label.hashCode +
        label2.hashCode +
        dn.hashCode +
        issue.hashCode +
        valid.hashCode +
        expire.hashCode +
        title.hashCode;

  factory SpcOutlookPayload.fromJson(Map<String, dynamic> json) => _$SpcOutlookPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$SpcOutlookPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SpcOutlookPayloadTypeEnum {
@JsonValue(r'cat')
cat(r'cat'),
@JsonValue(r'torn')
torn(r'torn'),
@JsonValue(r'wind')
wind(r'wind'),
@JsonValue(r'hail')
hail(r'hail'),
@JsonValue(r'sigtorn')
sigtorn(r'sigtorn'),
@JsonValue(r'sigwind')
sigwind(r'sigwind'),
@JsonValue(r'sighail')
sighail(r'sighail'),
@JsonValue(r'prob')
prob(r'prob'),
@JsonValue(r'sigprob')
sigprob(r'sigprob');

const SpcOutlookPayloadTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


