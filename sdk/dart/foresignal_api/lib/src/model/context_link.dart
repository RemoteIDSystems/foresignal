//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextLink {
  /// Returns a new [ContextLink] instance.
  ContextLink({

    required  this.version,

     this.workspaceId,

    required  this.payload,

    required  this.fragment,

    required  this.bytes,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final ContextLinkVersionEnum version;



      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: false,
    includeIfNull: false,
  )


  final String? workspaceId;



      /// base64url JSON `{\"v\":1,\"ws\":…,\"ctx\":{…}}`
  @JsonKey(
    
    name: r'payload',
    required: true,
    includeIfNull: false,
  )


  final String payload;



  @JsonKey(
    
    name: r'fragment',
    required: true,
    includeIfNull: false,
  )


  final String fragment;



          // maximum: 2048
  @JsonKey(
    
    name: r'bytes',
    required: true,
    includeIfNull: false,
  )


  final int bytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextLink &&
      other.version == version &&
      other.workspaceId == workspaceId &&
      other.payload == payload &&
      other.fragment == fragment &&
      other.bytes == bytes;

    @override
    int get hashCode =>
        version.hashCode +
        (workspaceId == null ? 0 : workspaceId.hashCode) +
        payload.hashCode +
        fragment.hashCode +
        bytes.hashCode;

  factory ContextLink.fromJson(Map<String, dynamic> json) => _$ContextLinkFromJson(json);

  Map<String, dynamic> toJson() => _$ContextLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ContextLinkVersionEnum {
@JsonValue(1)
number1('1');

const ContextLinkVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


