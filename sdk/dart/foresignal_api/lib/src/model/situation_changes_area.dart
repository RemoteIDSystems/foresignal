//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_changes_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationChangesArea {
  /// Returns a new [SituationChangesArea] instance.
  SituationChangesArea({

     this.beforeKm2,

     this.afterKm2,

     this.deltaKm2,
  });

  @JsonKey(
    
    name: r'beforeKm2',
    required: false,
    includeIfNull: false,
  )


  final num? beforeKm2;



  @JsonKey(
    
    name: r'afterKm2',
    required: false,
    includeIfNull: false,
  )


  final num? afterKm2;



  @JsonKey(
    
    name: r'deltaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? deltaKm2;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationChangesArea &&
      other.beforeKm2 == beforeKm2 &&
      other.afterKm2 == afterKm2 &&
      other.deltaKm2 == deltaKm2;

    @override
    int get hashCode =>
        (beforeKm2 == null ? 0 : beforeKm2.hashCode) +
        (afterKm2 == null ? 0 : afterKm2.hashCode) +
        (deltaKm2 == null ? 0 : deltaKm2.hashCode);

  factory SituationChangesArea.fromJson(Map<String, dynamic> json) => _$SituationChangesAreaFromJson(json);

  Map<String, dynamic> toJson() => _$SituationChangesAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

