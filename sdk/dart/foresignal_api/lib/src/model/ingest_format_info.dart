//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_format_option.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_format_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestFormatInfo {
  /// Returns a new [IngestFormatInfo] instance.
  IngestFormatInfo({

    required  this.id,

     this.description,

    required  this.mediaTypes,

     this.methods,

     this.options,

     this.verifiers,

     this.reserved,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'mediaTypes',
    required: true,
    includeIfNull: false,
  )


  final List<String> mediaTypes;



  @JsonKey(
    
    name: r'methods',
    required: false,
    includeIfNull: false,
  )


  final List<IngestFormatInfoMethodsEnum>? methods;



  @JsonKey(
    
    name: r'options',
    required: false,
    includeIfNull: false,
  )


  final List<IngestFormatOption>? options;



  @JsonKey(
    
    name: r'verifiers',
    required: false,
    includeIfNull: false,
  )


  final List<IngestFormatInfoVerifiersEnum>? verifiers;



      /// Name reserved for a later prompt (media, rasters: 054; mapping-preview: 059)
  @JsonKey(
    
    name: r'reserved',
    required: false,
    includeIfNull: false,
  )


  final bool? reserved;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestFormatInfo &&
      other.id == id &&
      other.description == description &&
      other.mediaTypes == mediaTypes &&
      other.methods == methods &&
      other.options == options &&
      other.verifiers == verifiers &&
      other.reserved == reserved;

    @override
    int get hashCode =>
        id.hashCode +
        description.hashCode +
        mediaTypes.hashCode +
        methods.hashCode +
        options.hashCode +
        verifiers.hashCode +
        reserved.hashCode;

  factory IngestFormatInfo.fromJson(Map<String, dynamic> json) => _$IngestFormatInfoFromJson(json);

  Map<String, dynamic> toJson() => _$IngestFormatInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestFormatInfoMethodsEnum {
@JsonValue(r'GET')
GET(r'GET'),
@JsonValue(r'POST')
POST(r'POST'),
@JsonValue(r'PUT')
PUT(r'PUT');

const IngestFormatInfoMethodsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IngestFormatInfoVerifiersEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'hmac-sha256')
hmacSha256(r'hmac-sha256'),
@JsonValue(r'standard-webhooks')
standardWebhooks(r'standard-webhooks'),
@JsonValue(r'foresignal')
foresignal(r'foresignal');

const IngestFormatInfoVerifiersEnum(this.value);

final String value;

@override
String toString() => value;
}


