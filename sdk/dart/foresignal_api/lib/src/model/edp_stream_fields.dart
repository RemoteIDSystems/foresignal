//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_stream_fields.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpStreamFields {
  /// Returns a new [EdpStreamFields] instance.
  EdpStreamFields({

     this.title,

     this.significant,

     this.volatile,

     this.observedAtMode,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'significant',
    required: false,
    includeIfNull: false,
  )


  final List<String>? significant;



  @JsonKey(
    
    name: r'volatile',
    required: false,
    includeIfNull: false,
  )


  final List<String>? volatile;



  @JsonKey(
    
    name: r'observedAtMode',
    required: false,
    includeIfNull: false,
  )


  final EdpStreamFieldsObservedAtModeEnum? observedAtMode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpStreamFields &&
      other.title == title &&
      other.significant == significant &&
      other.volatile == volatile &&
      other.observedAtMode == observedAtMode;

    @override
    int get hashCode =>
        (title == null ? 0 : title.hashCode) +
        significant.hashCode +
        volatile.hashCode +
        observedAtMode.hashCode;

  factory EdpStreamFields.fromJson(Map<String, dynamic> json) => _$EdpStreamFieldsFromJson(json);

  Map<String, dynamic> toJson() => _$EdpStreamFieldsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpStreamFieldsObservedAtModeEnum {
@JsonValue(r'upstream')
upstream(r'upstream'),
@JsonValue(r'received')
received(r'received');

const EdpStreamFieldsObservedAtModeEnum(this.value);

final String value;

@override
String toString() => value;
}


