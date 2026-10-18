//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scorecard_entry_bias.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ScorecardEntryBias {
  /// Returns a new [ScorecardEntryBias] instance.
  ScorecardEntryBias({

    required  this.meanForecastMinusObserved,

    required  this.hedgedShare,

     this.meanForecastOnNo,
  });

  @JsonKey(
    
    name: r'meanForecastMinusObserved',
    required: true,
    includeIfNull: false,
  )


  final num meanForecastMinusObserved;



      /// Share of forecasts in [0.4, 0.6]
  @JsonKey(
    
    name: r'hedgedShare',
    required: true,
    includeIfNull: false,
  )


  final num hedgedShare;



      /// Mean forecast on questions that resolved no
  @JsonKey(
    
    name: r'meanForecastOnNo',
    required: false,
    includeIfNull: false,
  )


  final num? meanForecastOnNo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ScorecardEntryBias &&
      other.meanForecastMinusObserved == meanForecastMinusObserved &&
      other.hedgedShare == hedgedShare &&
      other.meanForecastOnNo == meanForecastOnNo;

    @override
    int get hashCode =>
        meanForecastMinusObserved.hashCode +
        hedgedShare.hashCode +
        (meanForecastOnNo == null ? 0 : meanForecastOnNo.hashCode);

  factory ScorecardEntryBias.fromJson(Map<String, dynamic> json) => _$ScorecardEntryBiasFromJson(json);

  Map<String, dynamic> toJson() => _$ScorecardEntryBiasToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

