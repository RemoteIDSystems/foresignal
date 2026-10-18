//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_cursor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystCursor {
  /// Returns a new [AnalystCursor] instance.
  AnalystCursor({

     this.seq,

     this.alertId,

     this.eventUpdatedAt,

     this.anomalyId,
  });

  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



  @JsonKey(
    
    name: r'alertId',
    required: false,
    includeIfNull: false,
  )


  final int? alertId;



  @JsonKey(
    
    name: r'eventUpdatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventUpdatedAt;



  @JsonKey(
    
    name: r'anomalyId',
    required: false,
    includeIfNull: false,
  )


  final int? anomalyId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystCursor &&
      other.seq == seq &&
      other.alertId == alertId &&
      other.eventUpdatedAt == eventUpdatedAt &&
      other.anomalyId == anomalyId;

    @override
    int get hashCode =>
        (seq == null ? 0 : seq.hashCode) +
        (alertId == null ? 0 : alertId.hashCode) +
        (eventUpdatedAt == null ? 0 : eventUpdatedAt.hashCode) +
        (anomalyId == null ? 0 : anomalyId.hashCode);

  factory AnalystCursor.fromJson(Map<String, dynamic> json) => _$AnalystCursorFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystCursorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

