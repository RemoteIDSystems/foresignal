//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_frame_index_frames_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleFrameIndexFramesInner {
  /// Returns a new [ChronicleFrameIndexFramesInner] instance.
  ChronicleFrameIndexFramesInner({

    required  this.t,

    required  this.counts,

     this.changed,

     this.changedTotal,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



      /// Layer id → captured versions in the step
  @JsonKey(
    
    name: r'counts',
    required: true,
    includeIfNull: false,
  )


  final Map<String, int> counts;



      /// Changed entities (capped)
  @JsonKey(
    
    name: r'changed',
    required: false,
    includeIfNull: false,
  )


  final List<String>? changed;



  @JsonKey(
    
    name: r'changedTotal',
    required: false,
    includeIfNull: false,
  )


  final int? changedTotal;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleFrameIndexFramesInner &&
      other.t == t &&
      other.counts == counts &&
      other.changed == changed &&
      other.changedTotal == changedTotal;

    @override
    int get hashCode =>
        t.hashCode +
        counts.hashCode +
        changed.hashCode +
        changedTotal.hashCode;

  factory ChronicleFrameIndexFramesInner.fromJson(Map<String, dynamic> json) => _$ChronicleFrameIndexFramesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleFrameIndexFramesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

