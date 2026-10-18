//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'import_media_ratings_request1_rows_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ImportMediaRatingsRequest1RowsInner {
  /// Returns a new [ImportMediaRatingsRequest1RowsInner] instance.
  ImportMediaRatingsRequest1RowsInner({

    required  this.domain,

    required  this.dimension,

    required  this.value,

     this.label,

    required  this.licence,

    required  this.attribution,
  });

  @JsonKey(
    
    name: r'domain',
    required: true,
    includeIfNull: false,
  )


  final String domain;



      /// Rating dimension; provider axes that are not left–right are recorded as `{provider}:{axis}`, never forced into lean
  @JsonKey(
    
    name: r'dimension',
    required: true,
    includeIfNull: true,
  )


  final Object? dimension;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final String value;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final String attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ImportMediaRatingsRequest1RowsInner &&
      other.domain == domain &&
      other.dimension == dimension &&
      other.value == value &&
      other.label == label &&
      other.licence == licence &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        domain.hashCode +
        (dimension == null ? 0 : dimension.hashCode) +
        value.hashCode +
        (label == null ? 0 : label.hashCode) +
        licence.hashCode +
        attribution.hashCode;

  factory ImportMediaRatingsRequest1RowsInner.fromJson(Map<String, dynamic> json) => _$ImportMediaRatingsRequest1RowsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ImportMediaRatingsRequest1RowsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

