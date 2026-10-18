//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_rasters_render_queue.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemRastersRenderQueue {
  /// Returns a new [SystemRastersRenderQueue] instance.
  SystemRastersRenderQueue({

     this.depth,

     this.active,

     this.poolSize,
  });

  @JsonKey(
    
    name: r'depth',
    required: false,
    includeIfNull: false,
  )


  final int? depth;



  @JsonKey(
    
    name: r'active',
    required: false,
    includeIfNull: false,
  )


  final int? active;



  @JsonKey(
    
    name: r'poolSize',
    required: false,
    includeIfNull: false,
  )


  final int? poolSize;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemRastersRenderQueue &&
      other.depth == depth &&
      other.active == active &&
      other.poolSize == poolSize;

    @override
    int get hashCode =>
        depth.hashCode +
        active.hashCode +
        poolSize.hashCode;

  factory SystemRastersRenderQueue.fromJson(Map<String, dynamic> json) => _$SystemRastersRenderQueueFromJson(json);

  Map<String, dynamic> toJson() => _$SystemRastersRenderQueueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

