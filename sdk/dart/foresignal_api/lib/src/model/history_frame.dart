//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/history_frame_removals_inner.dart';
import 'package:foresignal_api/src/model/frame_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'history_frame.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HistoryFrame {
  /// Returns a new [HistoryFrame] instance.
  HistoryFrame({

    required  this.t,

    required  this.upserts,

    required  this.removals,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'upserts',
    required: true,
    includeIfNull: false,
  )


  final List<FrameItem> upserts;



  @JsonKey(
    
    name: r'removals',
    required: true,
    includeIfNull: false,
  )


  final List<HistoryFrameRemovalsInner> removals;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HistoryFrame &&
      other.t == t &&
      other.upserts == upserts &&
      other.removals == removals;

    @override
    int get hashCode =>
        t.hashCode +
        upserts.hashCode +
        removals.hashCode;

  factory HistoryFrame.fromJson(Map<String, dynamic> json) => _$HistoryFrameFromJson(json);

  Map<String, dynamic> toJson() => _$HistoryFrameToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

