//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_sample.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingSample {
  /// Returns a new [MappingSample] instance.
  MappingSample({

    required  this.contentType,

     this.body,

     this.query,
  });

  @JsonKey(
    
    name: r'contentType',
    required: true,
    includeIfNull: false,
  )


  final String contentType;



      /// Raw body as a string, or a JSON value for JSON samples
  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final Object? body;



      /// Raw query string or name→value map (GET protocols such as WU)
  @JsonKey(
    
    name: r'query',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? query;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingSample &&
      other.contentType == contentType &&
      other.body == body &&
      other.query == query;

    @override
    int get hashCode =>
        contentType.hashCode +
        (body == null ? 0 : body.hashCode) +
        (query == null ? 0 : query.hashCode);

  factory MappingSample.fromJson(Map<String, dynamic> json) => _$MappingSampleFromJson(json);

  Map<String, dynamic> toJson() => _$MappingSampleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

