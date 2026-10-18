//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stix_bundle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StixBundle {
  /// Returns a new [StixBundle] instance.
  StixBundle({

    required  this.type,

    required  this.id,

    required  this.objects,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final StixBundleTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'objects',
    required: true,
    includeIfNull: false,
  )


  final List<Map<String, Object>> objects;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StixBundle &&
      other.type == type &&
      other.id == id &&
      other.objects == objects;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode +
        objects.hashCode;

  factory StixBundle.fromJson(Map<String, dynamic> json) => _$StixBundleFromJson(json);

  Map<String, dynamic> toJson() => _$StixBundleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StixBundleTypeEnum {
@JsonValue(r'bundle')
bundle(r'bundle');

const StixBundleTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


