//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_history_series_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_history.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastHistory {
  /// Returns a new [ForecastHistory] instance.
  ForecastHistory({

    required  this.questionId,

    required  this.type,

    required  this.step,

     this.from,

     this.to,

    required  this.series,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ForecastHistoryTypeEnum type;



  @JsonKey(
    
    name: r'step',
    required: true,
    includeIfNull: false,
  )


  final ForecastHistoryStepEnum step;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastHistorySeriesInner> series;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastHistory &&
      other.questionId == questionId &&
      other.type == type &&
      other.step == step &&
      other.from == from &&
      other.to == to &&
      other.series == series;

    @override
    int get hashCode =>
        questionId.hashCode +
        type.hashCode +
        step.hashCode +
        from.hashCode +
        to.hashCode +
        series.hashCode;

  factory ForecastHistory.fromJson(Map<String, dynamic> json) => _$ForecastHistoryFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastHistoryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastHistoryTypeEnum {
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'multiple')
multiple(r'multiple'),
@JsonValue(r'ordered')
ordered(r'ordered'),
@JsonValue(r'numeric')
numeric(r'numeric');

const ForecastHistoryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastHistoryStepEnum {
@JsonValue(r'P1D')
P1D(r'P1D');

const ForecastHistoryStepEnum(this.value);

final String value;

@override
String toString() => value;
}


