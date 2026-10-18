//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_package_capture_span.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchPackageCaptureSpan {
  /// Returns a new [ChronicleSketchPackageCaptureSpan] instance.
  ChronicleSketchPackageCaptureSpan({

    required  this.from,

     this.to,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final String from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final String? to;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchPackageCaptureSpan &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        from.hashCode +
        (to == null ? 0 : to.hashCode);

  factory ChronicleSketchPackageCaptureSpan.fromJson(Map<String, dynamic> json) => _$ChronicleSketchPackageCaptureSpanFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchPackageCaptureSpanToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

