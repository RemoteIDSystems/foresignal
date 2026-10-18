//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'captured_sample.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CapturedSample {
  /// Returns a new [CapturedSample] instance.
  CapturedSample({

    required  this.receivedAt,

    required  this.method,

     this.format,

     this.contentType,

     this.contentEncoding,

     this.query,

     this.body,

     this.bodyBytes,

     this.truncated,
  });

  @JsonKey(
    
    name: r'receivedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime receivedAt;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final CapturedSampleMethodEnum method;



      /// Format path segment, null for the default endpoint
  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final String? format;



  @JsonKey(
    
    name: r'contentType',
    required: false,
    includeIfNull: false,
  )


  final String? contentType;



  @JsonKey(
    
    name: r'contentEncoding',
    required: false,
    includeIfNull: false,
  )


  final String? contentEncoding;



      /// Redacted query string
  @JsonKey(
    
    name: r'query',
    required: false,
    includeIfNull: false,
  )


  final String? query;



      /// Redacted, decompressed body (text)
  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



  @JsonKey(
    
    name: r'bodyBytes',
    required: false,
    includeIfNull: false,
  )


  final int? bodyBytes;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CapturedSample &&
      other.receivedAt == receivedAt &&
      other.method == method &&
      other.format == format &&
      other.contentType == contentType &&
      other.contentEncoding == contentEncoding &&
      other.query == query &&
      other.body == body &&
      other.bodyBytes == bodyBytes &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        receivedAt.hashCode +
        method.hashCode +
        (format == null ? 0 : format.hashCode) +
        (contentType == null ? 0 : contentType.hashCode) +
        (contentEncoding == null ? 0 : contentEncoding.hashCode) +
        (query == null ? 0 : query.hashCode) +
        (body == null ? 0 : body.hashCode) +
        bodyBytes.hashCode +
        truncated.hashCode;

  factory CapturedSample.fromJson(Map<String, dynamic> json) => _$CapturedSampleFromJson(json);

  Map<String, dynamic> toJson() => _$CapturedSampleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CapturedSampleMethodEnum {
@JsonValue(r'GET')
GET(r'GET'),
@JsonValue(r'POST')
POST(r'POST'),
@JsonValue(r'PUT')
PUT(r'PUT');

const CapturedSampleMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


