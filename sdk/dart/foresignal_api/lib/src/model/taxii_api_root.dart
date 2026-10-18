//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taxii_api_root.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TaxiiApiRoot {
  /// Returns a new [TaxiiApiRoot] instance.
  TaxiiApiRoot({

    required  this.title,

     this.description,

    required  this.versions,

    required  this.maxContentLength,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'versions',
    required: true,
    includeIfNull: false,
  )


  final List<String> versions;



  @JsonKey(
    
    name: r'max_content_length',
    required: true,
    includeIfNull: false,
  )


  final int maxContentLength;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TaxiiApiRoot &&
      other.title == title &&
      other.description == description &&
      other.versions == versions &&
      other.maxContentLength == maxContentLength;

    @override
    int get hashCode =>
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        versions.hashCode +
        maxContentLength.hashCode;

  factory TaxiiApiRoot.fromJson(Map<String, dynamic> json) => _$TaxiiApiRootFromJson(json);

  Map<String, dynamic> toJson() => _$TaxiiApiRootToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

