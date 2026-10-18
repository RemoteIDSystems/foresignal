//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_frame_index_frames_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_frame_index.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleFrameIndex {
  /// Returns a new [ChronicleFrameIndex] instance.
  ChronicleFrameIndex({

    required  this.chronicleId,

    required  this.from,

    required  this.to,

    required  this.step,

     this.layers,

    required  this.frames,

     this.truncated,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'step',
    required: true,
    includeIfNull: false,
  )


  final String step;



  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;



  @JsonKey(
    
    name: r'frames',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleFrameIndexFramesInner> frames;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleFrameIndex &&
      other.chronicleId == chronicleId &&
      other.from == from &&
      other.to == to &&
      other.step == step &&
      other.layers == layers &&
      other.frames == frames &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        from.hashCode +
        to.hashCode +
        step.hashCode +
        layers.hashCode +
        frames.hashCode +
        truncated.hashCode;

  factory ChronicleFrameIndex.fromJson(Map<String, dynamic> json) => _$ChronicleFrameIndexFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleFrameIndexToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

