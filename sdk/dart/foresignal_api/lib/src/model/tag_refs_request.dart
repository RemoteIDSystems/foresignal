//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag_refs_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TagRefsRequest {
  /// Returns a new [TagRefsRequest] instance.
  TagRefsRequest({

    required  this.refs,
  });

  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TagRefsRequest &&
      other.refs == refs;

    @override
    int get hashCode =>
        refs.hashCode;

  factory TagRefsRequest.fromJson(Map<String, dynamic> json) => _$TagRefsRequestFromJson(json);

  Map<String, dynamic> toJson() => _$TagRefsRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

