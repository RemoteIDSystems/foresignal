//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_packet_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet_outlooks.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacketOutlooks {
  /// Returns a new [CyclePacketOutlooks] instance.
  CyclePacketOutlooks({

     this.indexChanges,

     this.windowsOutsideP10P90,
  });

  @JsonKey(
    
    name: r'indexChanges',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? indexChanges;



  @JsonKey(
    
    name: r'windowsOutsideP10P90',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? windowsOutsideP10P90;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacketOutlooks &&
      other.indexChanges == indexChanges &&
      other.windowsOutsideP10P90 == windowsOutsideP10P90;

    @override
    int get hashCode =>
        indexChanges.hashCode +
        windowsOutsideP10P90.hashCode;

  factory CyclePacketOutlooks.fromJson(Map<String, dynamic> json) => _$CyclePacketOutlooksFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketOutlooksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

