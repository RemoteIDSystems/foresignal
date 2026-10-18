//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_coverage.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_coverage_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCoveragePage {
  /// Returns a new [SocialCoveragePage] instance.
  SocialCoveragePage({

    required  this.items,

    required  this.nextCursor,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SocialCoverage> items;



      /// Opaque cursor for the next page; null at the end
  @JsonKey(
    
    name: r'nextCursor',
    required: true,
    includeIfNull: true,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCoveragePage &&
      other.items == items &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory SocialCoveragePage.fromJson(Map<String, dynamic> json) => _$SocialCoveragePageFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCoveragePageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

