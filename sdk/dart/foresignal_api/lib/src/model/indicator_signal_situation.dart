//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_signal_situation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorSignalSituation {
  /// Returns a new [IndicatorSignalSituation] instance.
  IndicatorSignalSituation({

    required  this.situationId,

     this.status,

     this.minSeverity,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: true,
    includeIfNull: false,
  )


  final String situationId;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalSituationStatusEnum? status;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? minSeverity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorSignalSituation &&
      other.situationId == situationId &&
      other.status == status &&
      other.minSeverity == minSeverity;

    @override
    int get hashCode =>
        situationId.hashCode +
        (status == null ? 0 : status.hashCode) +
        (minSeverity == null ? 0 : minSeverity.hashCode);

  factory IndicatorSignalSituation.fromJson(Map<String, dynamic> json) => _$IndicatorSignalSituationFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorSignalSituationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorSignalSituationStatusEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'dormant')
dormant(r'dormant'),
@JsonValue(r'closed')
closed(r'closed');

const IndicatorSignalSituationStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


