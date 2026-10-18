//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/relation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'related_object.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RelatedObject {
  /// Returns a new [RelatedObject] instance.
  RelatedObject({

    required  this.relation,

    required  this.direction,

    required  this.object,

     this.distanceKm,
  });

  @JsonKey(
    
    name: r'relation',
    required: true,
    includeIfNull: false,
  )


  final Relation relation;



  @JsonKey(
    
    name: r'direction',
    required: true,
    includeIfNull: false,
  )


  final RelatedObjectDirectionEnum direction;



  @JsonKey(
    
    name: r'object',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope object;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RelatedObject &&
      other.relation == relation &&
      other.direction == direction &&
      other.object == object &&
      other.distanceKm == distanceKm;

    @override
    int get hashCode =>
        relation.hashCode +
        direction.hashCode +
        object.hashCode +
        (distanceKm == null ? 0 : distanceKm.hashCode);

  factory RelatedObject.fromJson(Map<String, dynamic> json) => _$RelatedObjectFromJson(json);

  Map<String, dynamic> toJson() => _$RelatedObjectToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RelatedObjectDirectionEnum {
@JsonValue(r'out')
out_(r'out'),
@JsonValue(r'in')
in_(r'in');

const RelatedObjectDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


