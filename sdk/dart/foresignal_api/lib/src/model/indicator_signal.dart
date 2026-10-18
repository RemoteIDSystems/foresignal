//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/trigger_rule_input.dart';
import 'package:foresignal_api/src/model/indicator_signal_series.dart';
import 'package:foresignal_api/src/model/indicator_signal_situation.dart';
import 'package:foresignal_api/src/model/indicator_signal_question.dart';
import 'package:foresignal_api/src/model/indicator_signal_intel_event.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_signal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorSignal {
  /// Returns a new [IndicatorSignal] instance.
  IndicatorSignal({

    required  this.type,

     this.rule,

     this.precursorId,

     this.series,

     this.intelEvent,

     this.situation,

     this.question,

     this.emerging,

     this.baseRate,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IndicatorSignalTypeEnum type;



  @JsonKey(
    
    name: r'rule',
    required: false,
    includeIfNull: false,
  )


  final TriggerRuleInput? rule;



      /// 120 precursor id (subscribes to `precursor-active` findings)
  @JsonKey(
    
    name: r'precursorId',
    required: false,
    includeIfNull: false,
  )


  final String? precursorId;



  @JsonKey(
    
    name: r'series',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalSeries? series;



  @JsonKey(
    
    name: r'intelEvent',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalIntelEvent? intelEvent;



  @JsonKey(
    
    name: r'situation',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalSituation? situation;



  @JsonKey(
    
    name: r'question',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalQuestion? question;



  @JsonKey(
    
    name: r'emerging',
    required: false,
    includeIfNull: false,
  )


  final Object? emerging;



      /// Base rate of the signal when the hypotheses say nothing about it, used for information value (default 0.5)
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorSignal &&
      other.type == type &&
      other.rule == rule &&
      other.precursorId == precursorId &&
      other.series == series &&
      other.intelEvent == intelEvent &&
      other.situation == situation &&
      other.question == question &&
      other.emerging == emerging &&
      other.baseRate == baseRate;

    @override
    int get hashCode =>
        type.hashCode +
        (rule == null ? 0 : rule.hashCode) +
        (precursorId == null ? 0 : precursorId.hashCode) +
        (series == null ? 0 : series.hashCode) +
        (intelEvent == null ? 0 : intelEvent.hashCode) +
        (situation == null ? 0 : situation.hashCode) +
        (question == null ? 0 : question.hashCode) +
        (emerging == null ? 0 : emerging.hashCode) +
        (baseRate == null ? 0 : baseRate.hashCode);

  factory IndicatorSignal.fromJson(Map<String, dynamic> json) => _$IndicatorSignalFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorSignalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorSignalTypeEnum {
@JsonValue(r'rule')
rule(r'rule'),
@JsonValue(r'precursor')
precursor(r'precursor'),
@JsonValue(r'series')
series(r'series'),
@JsonValue(r'intelEvent')
intelEvent(r'intelEvent'),
@JsonValue(r'situation')
situation(r'situation'),
@JsonValue(r'question')
question(r'question'),
@JsonValue(r'manual')
manual(r'manual');

const IndicatorSignalTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


