//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_changes_new_members.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationChangesNewMembers {
  /// Returns a new [SituationChangesNewMembers] instance.
  SituationChangesNewMembers({

    required  this.total,

    required  this.byFamily,

     this.byType,
  });

  @JsonKey(
    
    name: r'total',
    required: true,
    includeIfNull: false,
  )


  final int total;



      /// Count per signal family
  @JsonKey(
    
    name: r'byFamily',
    required: true,
    includeIfNull: false,
  )


  final Map<String, int> byFamily;



      /// Count per member type
  @JsonKey(
    
    name: r'byType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byType;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationChangesNewMembers &&
      other.total == total &&
      other.byFamily == byFamily &&
      other.byType == byType;

    @override
    int get hashCode =>
        total.hashCode +
        byFamily.hashCode +
        byType.hashCode;

  factory SituationChangesNewMembers.fromJson(Map<String, dynamic> json) => _$SituationChangesNewMembersFromJson(json);

  Map<String, dynamic> toJson() => _$SituationChangesNewMembersToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

