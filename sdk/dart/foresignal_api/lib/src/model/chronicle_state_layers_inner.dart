//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_record_version.dart';
import 'package:foresignal_api/src/model/chronicle_state_layers_inner_rollups_inner.dart';
import 'package:foresignal_api/src/model/capture_mode.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_state_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleStateLayersInner {
  /// Returns a new [ChronicleStateLayersInner] instance.
  ChronicleStateLayersInner({

    required  this.layerId,

     this.name,

     this.capture,

    required  this.count,

    required  this.items,

     this.rollups,

    required  this.nextCursor,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'layerId',
    required: true,
    includeIfNull: false,
  )


  final String layerId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'capture',
    required: false,
    includeIfNull: false,
  )


  final CaptureMode? capture;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleRecordVersion> items;



  @JsonKey(
    
    name: r'rollups',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleStateLayersInnerRollupsInner>? rollups;



  @JsonKey(
    
    name: r'nextCursor',
    required: true,
    includeIfNull: true,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleStateLayersInner &&
      other.layerId == layerId &&
      other.name == name &&
      other.capture == capture &&
      other.count == count &&
      other.items == items &&
      other.rollups == rollups &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        layerId.hashCode +
        name.hashCode +
        capture.hashCode +
        count.hashCode +
        items.hashCode +
        rollups.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory ChronicleStateLayersInner.fromJson(Map<String, dynamic> json) => _$ChronicleStateLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleStateLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

