//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecaster_track_record.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecasterTrackRecord {
  /// Returns a new [ForecasterTrackRecord] instance.
  ForecasterTrackRecord({

    required  this.nResolved,

     this.brier,

    required  this.label,
  });

  @JsonKey(
    
    name: r'nResolved',
    required: true,
    includeIfNull: false,
  )


  final int nResolved;



  @JsonKey(
    
    name: r'brier',
    required: false,
    includeIfNull: false,
  )


  final num? brier;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final ForecasterTrackRecordLabelEnum label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecasterTrackRecord &&
      other.nResolved == nResolved &&
      other.brier == brier &&
      other.label == label;

    @override
    int get hashCode =>
        nResolved.hashCode +
        (brier == null ? 0 : brier.hashCode) +
        label.hashCode;

  factory ForecasterTrackRecord.fromJson(Map<String, dynamic> json) => _$ForecasterTrackRecordFromJson(json);

  Map<String, dynamic> toJson() => _$ForecasterTrackRecordToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecasterTrackRecordLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const ForecasterTrackRecordLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


