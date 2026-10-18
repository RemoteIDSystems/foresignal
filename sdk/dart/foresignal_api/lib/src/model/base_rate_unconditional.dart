//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'base_rate_unconditional.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaseRateUnconditional {
  /// Returns a new [BaseRateUnconditional] instance.
  BaseRateUnconditional({

    required  this.ratePerWindow,

    required  this.pAtLeastOne,

    required  this.interval,

     this.nWindows,
  });

      /// Mean events per window
  @JsonKey(
    
    name: r'ratePerWindow',
    required: true,
    includeIfNull: false,
  )


  final num ratePerWindow;



      /// Fraction of windows with at least one event
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pAtLeastOne',
    required: true,
    includeIfNull: false,
  )


  final num pAtLeastOne;



  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final StatInterval interval;



      /// Windows behind the unconditional rate
  @JsonKey(
    
    name: r'nWindows',
    required: false,
    includeIfNull: false,
  )


  final int? nWindows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaseRateUnconditional &&
      other.ratePerWindow == ratePerWindow &&
      other.pAtLeastOne == pAtLeastOne &&
      other.interval == interval &&
      other.nWindows == nWindows;

    @override
    int get hashCode =>
        ratePerWindow.hashCode +
        pAtLeastOne.hashCode +
        interval.hashCode +
        nWindows.hashCode;

  factory BaseRateUnconditional.fromJson(Map<String, dynamic> json) => _$BaseRateUnconditionalFromJson(json);

  Map<String, dynamic> toJson() => _$BaseRateUnconditionalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

