//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerStats {
  /// Returns a new [ChronicleLayerStats] instance.
  ChronicleLayerStats({

     this.rows,

     this.bytes,
  });

  @JsonKey(
    
    name: r'rows',
    required: false,
    includeIfNull: false,
  )


  final int? rows;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerStats &&
      other.rows == rows &&
      other.bytes == bytes;

    @override
    int get hashCode =>
        rows.hashCode +
        bytes.hashCode;

  factory ChronicleLayerStats.fromJson(Map<String, dynamic> json) => _$ChronicleLayerStatsFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

