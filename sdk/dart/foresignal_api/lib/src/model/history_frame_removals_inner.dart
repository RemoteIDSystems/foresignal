//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'history_frame_removals_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HistoryFrameRemovalsInner {
  /// Returns a new [HistoryFrameRemovalsInner] instance.
  HistoryFrameRemovalsInner({

    required  this.id,

    required  this.reason,

    required  this.left,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final HistoryFrameRemovalsInnerReasonEnum reason;



      /// True when the entity left the bbox
  @JsonKey(
    
    name: r'left',
    required: true,
    includeIfNull: false,
  )


  final bool left;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HistoryFrameRemovalsInner &&
      other.id == id &&
      other.reason == reason &&
      other.left == left;

    @override
    int get hashCode =>
        id.hashCode +
        reason.hashCode +
        left.hashCode;

  factory HistoryFrameRemovalsInner.fromJson(Map<String, dynamic> json) => _$HistoryFrameRemovalsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$HistoryFrameRemovalsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HistoryFrameRemovalsInnerReasonEnum {
@JsonValue(r'tombstone')
tombstone(r'tombstone'),
@JsonValue(r'expired')
expired(r'expired'),
@JsonValue(r'left')
left(r'left');

const HistoryFrameRemovalsInnerReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


