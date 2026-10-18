//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/saved_search_query.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchPatch {
  /// Returns a new [SavedSearchPatch] instance.
  SavedSearchPatch({

     this.name,

     this.query,

     this.visibility,

     this.tlp,

     this.notify,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'query',
    required: false,
    includeIfNull: false,
  )


  final SavedSearchQuery? query;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'notify',
    required: false,
    includeIfNull: false,
  )


  final bool? notify;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchPatch &&
      other.name == name &&
      other.query == query &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.notify == notify;

    @override
    int get hashCode =>
        name.hashCode +
        query.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        notify.hashCode;

  factory SavedSearchPatch.fromJson(Map<String, dynamic> json) => _$SavedSearchPatchFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

