//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_run_input_counts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystRunInputCounts {
  /// Returns a new [AnalystRunInputCounts] instance.
  AnalystRunInputCounts({

     this.events,

     this.anomalies,

     this.alerts,
  });

  @JsonKey(
    
    name: r'events',
    required: false,
    includeIfNull: false,
  )


  final int? events;



  @JsonKey(
    
    name: r'anomalies',
    required: false,
    includeIfNull: false,
  )


  final int? anomalies;



  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final int? alerts;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystRunInputCounts &&
      other.events == events &&
      other.anomalies == anomalies &&
      other.alerts == alerts;

    @override
    int get hashCode =>
        events.hashCode +
        anomalies.hashCode +
        alerts.hashCode;

  factory AnalystRunInputCounts.fromJson(Map<String, dynamic> json) => _$AnalystRunInputCountsFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystRunInputCountsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

