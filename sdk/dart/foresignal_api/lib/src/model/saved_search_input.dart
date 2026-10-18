//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/saved_search_query.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchInput {
  /// Returns a new [SavedSearchInput] instance.
  SavedSearchInput({

    required  this.name,

    required  this.query,

     this.visibility,

     this.tlp,

     this.notify = false,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'query',
    required: true,
    includeIfNull: false,
  )


  final SavedSearchQuery query;



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
    defaultValue: false,
    name: r'notify',
    required: false,
    includeIfNull: false,
  )


  final bool? notify;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchInput &&
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

  factory SavedSearchInput.fromJson(Map<String, dynamic> json) => _$SavedSearchInputFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

