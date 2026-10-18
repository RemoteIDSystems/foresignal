//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_protocol_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpProtocolInfo {
  /// Returns a new [EdpProtocolInfo] instance.
  EdpProtocolInfo({

    required  this.protocol,

     this.supported,

    required  this.itemTypes,

    required  this.modes,

     this.schemas,

     this.documentation,

     this.maxMinor,
  });

  @JsonKey(
    
    name: r'protocol',
    required: true,
    includeIfNull: false,
  )


  final String protocol;



  @JsonKey(
    
    name: r'supported',
    required: false,
    includeIfNull: false,
  )


  final List<String>? supported;



  @JsonKey(
    
    name: r'itemTypes',
    required: true,
    includeIfNull: false,
  )


  final List<String> itemTypes;



  @JsonKey(
    
    name: r'modes',
    required: true,
    includeIfNull: false,
  )


  final List<EdpProtocolInfoModesEnum> modes;



  @JsonKey(
    
    name: r'schemas',
    required: false,
    includeIfNull: false,
  )


  final List<String>? schemas;



  @JsonKey(
    
    name: r'documentation',
    required: false,
    includeIfNull: false,
  )


  final String? documentation;



      /// Highest 1.x minor version accepted
  @JsonKey(
    
    name: r'maxMinor',
    required: false,
    includeIfNull: false,
  )


  final int? maxMinor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpProtocolInfo &&
      other.protocol == protocol &&
      other.supported == supported &&
      other.itemTypes == itemTypes &&
      other.modes == modes &&
      other.schemas == schemas &&
      other.documentation == documentation &&
      other.maxMinor == maxMinor;

    @override
    int get hashCode =>
        protocol.hashCode +
        supported.hashCode +
        itemTypes.hashCode +
        modes.hashCode +
        schemas.hashCode +
        documentation.hashCode +
        maxMinor.hashCode;

  factory EdpProtocolInfo.fromJson(Map<String, dynamic> json) => _$EdpProtocolInfoFromJson(json);

  Map<String, dynamic> toJson() => _$EdpProtocolInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpProtocolInfoModesEnum {
@JsonValue(r'layer')
layer(r'layer'),
@JsonValue(r'atomic')
atomic(r'atomic');

const EdpProtocolInfoModesEnum(this.value);

final String value;

@override
String toString() => value;
}


