//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_load_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceLoadRequest {
  /// Returns a new [ReferenceLoadRequest] instance.
  ReferenceLoadRequest({

    required  this.datasets,
  });

  @JsonKey(
    
    name: r'datasets',
    required: true,
    includeIfNull: false,
  )


  final List<String> datasets;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceLoadRequest &&
      other.datasets == datasets;

    @override
    int get hashCode =>
        datasets.hashCode;

  factory ReferenceLoadRequest.fromJson(Map<String, dynamic> json) => _$ReferenceLoadRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceLoadRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

