//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_dataset.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceDataset {
  /// Returns a new [ReferenceDataset] instance.
  ReferenceDataset({

    required  this.name,

     this.edition,

     this.url,

     this.sha256,

     this.licence,

     this.attribution,

     this.rows,

     this.loadedAt,

     this.optional,

    required  this.state,

     this.error,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'edition',
    required: false,
    includeIfNull: false,
  )


  final String? edition;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'sha256',
    required: false,
    includeIfNull: false,
  )


  final String? sha256;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'rows',
    required: false,
    includeIfNull: false,
  )


  final int? rows;



  @JsonKey(
    
    name: r'loadedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? loadedAt;



  @JsonKey(
    
    name: r'optional',
    required: false,
    includeIfNull: false,
  )


  final bool? optional;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final ReferenceDatasetStateEnum state;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceDataset &&
      other.name == name &&
      other.edition == edition &&
      other.url == url &&
      other.sha256 == sha256 &&
      other.licence == licence &&
      other.attribution == attribution &&
      other.rows == rows &&
      other.loadedAt == loadedAt &&
      other.optional == optional &&
      other.state == state &&
      other.error == error;

    @override
    int get hashCode =>
        name.hashCode +
        (edition == null ? 0 : edition.hashCode) +
        url.hashCode +
        sha256.hashCode +
        licence.hashCode +
        (attribution == null ? 0 : attribution.hashCode) +
        (rows == null ? 0 : rows.hashCode) +
        (loadedAt == null ? 0 : loadedAt.hashCode) +
        optional.hashCode +
        state.hashCode +
        (error == null ? 0 : error.hashCode);

  factory ReferenceDataset.fromJson(Map<String, dynamic> json) => _$ReferenceDatasetFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceDatasetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReferenceDatasetStateEnum {
@JsonValue(r'absent')
absent(r'absent'),
@JsonValue(r'loading')
loading(r'loading'),
@JsonValue(r'loaded')
loaded(r'loaded'),
@JsonValue(r'failed')
failed(r'failed');

const ReferenceDatasetStateEnum(this.value);

final String value;

@override
String toString() => value;
}


