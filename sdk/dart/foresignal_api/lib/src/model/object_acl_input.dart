//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_acl_entry_input.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_acl_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectAclInput {
  /// Returns a new [ObjectAclInput] instance.
  ObjectAclInput({

     this.visibility,

    required  this.entries,

     this.owner,

     this.tlp,
  });

  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'entries',
    required: true,
    includeIfNull: false,
  )


  final List<ObjectAclEntryInput> entries;



      /// Principal id or handle of the new owner (owners only)
  @JsonKey(
    
    name: r'owner',
    required: false,
    includeIfNull: false,
  )


  final String? owner;



      /// Change the TLP label (owner or editor); grants and visibility are checked against the new label
  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectAclInput &&
      other.visibility == visibility &&
      other.entries == entries &&
      other.owner == owner &&
      other.tlp == tlp;

    @override
    int get hashCode =>
        visibility.hashCode +
        entries.hashCode +
        owner.hashCode +
        tlp.hashCode;

  factory ObjectAclInput.fromJson(Map<String, dynamic> json) => _$ObjectAclInputFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectAclInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

