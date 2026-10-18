//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_citation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskCitation {
  /// Returns a new [AskCitation] instance.
  AskCitation({

    required  this.id,

     this.seq,

    required  this.title,

     this.type,

     this.productId,

     this.frameKey,

     this.run,

     this.step,

     this.validTime,

     this.ageMinutes,
  });

      /// <sourceId>:<externalId>, briefing:<id>, alert:<id>, or (056) wx:<productId>@<frameKey> for a weather frame and wx:<productId>@<run>/<step> for a forecast step (GFS and HRRR providers: wx:gfs@<run>/<step>, NWS: wx:nws@<updatedAt>/<hours>)
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// weather (056): a value a weather tool returned; productId, frameKey, run, step, validTime and ageMinutes are set only for this type
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final AskCitationTypeEnum? type;



  @JsonKey(
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final String? productId;



  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



  @JsonKey(
    
    name: r'validTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTime;



  @JsonKey(
    
    name: r'ageMinutes',
    required: false,
    includeIfNull: false,
  )


  final num? ageMinutes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskCitation &&
      other.id == id &&
      other.seq == seq &&
      other.title == title &&
      other.type == type &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.run == run &&
      other.step == step &&
      other.validTime == validTime &&
      other.ageMinutes == ageMinutes;

    @override
    int get hashCode =>
        id.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        title.hashCode +
        type.hashCode +
        (productId == null ? 0 : productId.hashCode) +
        (frameKey == null ? 0 : frameKey.hashCode) +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        (validTime == null ? 0 : validTime.hashCode) +
        (ageMinutes == null ? 0 : ageMinutes.hashCode);

  factory AskCitation.fromJson(Map<String, dynamic> json) => _$AskCitationFromJson(json);

  Map<String, dynamic> toJson() => _$AskCitationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// weather (056): a value a weather tool returned; productId, frameKey, run, step, validTime and ageMinutes are set only for this type
enum AskCitationTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'weather')
weather(r'weather');

const AskCitationTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


