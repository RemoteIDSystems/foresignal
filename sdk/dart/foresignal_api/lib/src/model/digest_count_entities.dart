//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_count_entities.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestCountEntities {
  /// Returns a new [DigestCountEntities] instance.
  DigestCountEntities({

     this.new_,

     this.changed,

     this.removed,

     this.edges,
  });

  @JsonKey(
    
    name: r'new',
    required: false,
    includeIfNull: false,
  )


  final int? new_;



  @JsonKey(
    
    name: r'changed',
    required: false,
    includeIfNull: false,
  )


  final int? changed;



  @JsonKey(
    
    name: r'removed',
    required: false,
    includeIfNull: false,
  )


  final int? removed;



  @JsonKey(
    
    name: r'edges',
    required: false,
    includeIfNull: false,
  )


  final int? edges;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestCountEntities &&
      other.new_ == new_ &&
      other.changed == changed &&
      other.removed == removed &&
      other.edges == edges;

    @override
    int get hashCode =>
        new_.hashCode +
        changed.hashCode +
        removed.hashCode +
        edges.hashCode;

  factory DigestCountEntities.fromJson(Map<String, dynamic> json) => _$DigestCountEntitiesFromJson(json);

  Map<String, dynamic> toJson() => _$DigestCountEntitiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

