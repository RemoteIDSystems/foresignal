//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'caveat.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Caveat {
  /// Returns a new [Caveat] instance.
  Caveat({

    required  this.code,

    required  this.text,

     this.data,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Caveat &&
      other.code == code &&
      other.text == text &&
      other.data == data;

    @override
    int get hashCode =>
        code.hashCode +
        text.hashCode +
        data.hashCode;

  factory Caveat.fromJson(Map<String, dynamic> json) => _$CaveatFromJson(json);

  Map<String, dynamic> toJson() => _$CaveatToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

